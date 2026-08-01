const API_BASE = 'http://localhost:8080/api/notes';

const notesGrid = document.getElementById('notesGrid');
const noteModal = document.getElementById('noteModal');
const noteForm = document.getElementById('noteForm');
const modalTitle = document.getElementById('modalTitle');
const noteCount = document.getElementById('noteCount');
const noResults = document.getElementById('noResults');
const loadingIndicator = document.getElementById('loadingIndicator');
const currentFileInfo = document.getElementById('currentFileInfo');

let editingNoteId = null;

document.addEventListener('DOMContentLoaded', loadAllNotes);

async function loadAllNotes() {
    showLoading(true);
    try {
        const res = await fetch(API_BASE);
        const notes = await res.json();
        renderNotes(notes);
    } catch (err) {
        showToast('Failed to load notes', 'error');
    }
    showLoading(false);
}

function renderNotes(notes) {
    notesGrid.innerHTML = '';
    noteCount.textContent = notes.length;

    if (notes.length === 0) {
        noResults.classList.remove('hidden');
        return;
    }
    noResults.classList.add('hidden');

    notes.forEach(note => {
        const card = document.createElement('div');
        card.className = 'note-card';
        card.innerHTML = `
            <h3>${escapeHtml(note.title)}</h3>
            <p>${escapeHtml(note.description || 'No description provided')}</p>
            <div class="note-tags">
                <span class="tag">${escapeHtml(note.subject)}</span>
                <span class="tag">Sem ${escapeHtml(note.semester)}</span>
                <span class="tag">${escapeHtml(note.department)}</span>
            </div>
            <div class="note-meta">
                Uploaded by ${escapeHtml(note.uploadedBy)} on ${formatDate(note.uploadDate)}
                ${note.fileSize ? `- ${formatFileSize(note.fileSize)}` : ''}
            </div>
            <div class="note-actions">
                ${note.fileName ? `<button class="btn btn-secondary" onclick="downloadNote(${note.id})"><i class="fa-solid fa-download"></i> Download</button>` : ''}
                <button class="btn btn-outline" onclick="editNote(${note.id})"><i class="fa-solid fa-pen"></i> Edit</button>
                <button class="btn btn-danger" onclick="deleteNote(${note.id})"><i class="fa-solid fa-trash"></i></button>
            </div>
        `;
        notesGrid.appendChild(card);
    });
}

document.getElementById('searchBtn').addEventListener('click', async () => {
    const keyword = document.getElementById('searchKeyword').value.trim();
    const subject = document.getElementById('searchSubject').value.trim();
    const semester = document.getElementById('searchSemester').value;
    const department = document.getElementById('searchDepartment').value;

    showLoading(true);
    try {
        let url;
        if (keyword) {
            url = `${API_BASE}/search/keyword?keyword=${encodeURIComponent(keyword)}`;
        } else {
            const params = new URLSearchParams();
            if (subject) params.append('subject', subject);
            if (semester) params.append('semester', semester);
            if (department) params.append('department', department);
            url = `${API_BASE}/search?${params.toString()}`;
        }
        const res = await fetch(url);
        const notes = await res.json();
        renderNotes(notes);
    } catch (err) {
        showToast('Search failed', 'error');
    }
    showLoading(false);
});

document.getElementById('clearBtn').addEventListener('click', () => {
    document.getElementById('searchKeyword').value = '';
    document.getElementById('searchSubject').value = '';
    document.getElementById('searchSemester').value = '';
    document.getElementById('searchDepartment').value = '';
    loadAllNotes();
});

document.getElementById('uploadBtn').addEventListener('click', () => openModal());
document.getElementById('closeModal').addEventListener('click', closeModal);
document.getElementById('cancelBtn').addEventListener('click', closeModal);

function openModal(note = null) {
    noteForm.reset();
    currentFileInfo.textContent = '';
    editingNoteId = null;

    if (note) {
        modalTitle.textContent = 'Edit Note';
        editingNoteId = note.id;
        document.getElementById('title').value = note.title;
        document.getElementById('description').value = note.description || '';
        document.getElementById('subject').value = note.subject;
        document.getElementById('semester').value = note.semester;
        document.getElementById('department').value = note.department;
        document.getElementById('uploadedBy').value = note.uploadedBy;
        if (note.fileName) {
            currentFileInfo.textContent = `Current file: ${note.fileName} (upload new to replace)`;
        }
    } else {
        modalTitle.textContent = 'Upload New Note';
    }

    noteModal.classList.remove('hidden');
}

function closeModal() {
    noteModal.classList.add('hidden');
    editingNoteId = null;
}

noteForm.addEventListener('submit', async (e) => {
    e.preventDefault();

    const formData = new FormData();
    formData.append('title', document.getElementById('title').value);
    formData.append('description', document.getElementById('description').value);
    formData.append('subject', document.getElementById('subject').value);
    formData.append('semester', document.getElementById('semester').value);
    formData.append('department', document.getElementById('department').value);
    formData.append('uploadedBy', document.getElementById('uploadedBy').value);

    const fileInput = document.getElementById('fileInput');
    if (fileInput.files.length > 0) {
        formData.append('file', fileInput.files[0]);
    }

    try {
        let res;
        if (editingNoteId) {
            res = await fetch(`${API_BASE}/${editingNoteId}`, { method: 'PUT', body: formData });
        } else {
            res = await fetch(API_BASE, { method: 'POST', body: formData });
        }

        if (!res.ok) {
            const err = await res.json();
            throw new Error(err.message || 'Operation failed');
        }

        showToast(editingNoteId ? 'Note updated successfully!' : 'Note uploaded successfully!', 'success');
        closeModal();
        loadAllNotes();
    } catch (err) {
        showToast(err.message, 'error');
    }
});

async function editNote(id) {
    try {
        const res = await fetch(`${API_BASE}/${id}`);
        const note = await res.json();
        openModal(note);
    } catch (err) {
        showToast('Failed to load note details', 'error');
    }
}

async function deleteNote(id) {
    if (!confirm('Are you sure you want to delete this note?')) return;

    try {
        const res = await fetch(`${API_BASE}/${id}`, { method: 'DELETE' });
        if (!res.ok) throw new Error('Delete failed');
        showToast('Note deleted successfully!', 'success');
        loadAllNotes();
    } catch (err) {
        showToast('Failed to delete note', 'error');
    }
}

function downloadNote(id) {
    window.location.href = `${API_BASE}/download/${id}`;
}

function showLoading(show) {
    loadingIndicator.classList.toggle('hidden', !show);
}

function showToast(message, type = 'success') {
    const toast = document.getElementById('toast');
    toast.textContent = message;
    toast.className = `toast ${type}`;
    toast.classList.remove('hidden');
    setTimeout(() => toast.classList.add('hidden'), 3000);
}

function formatDate(dateStr) {
    const date = new Date(dateStr);
    return date.toLocaleDateString('en-US', { year: 'numeric', month: 'short', day: 'numeric' });
}

function formatFileSize(bytes) {
    if (bytes < 1024) return bytes + ' B';
    if (bytes < 1024 * 1024) return (bytes / 1024).toFixed(1) + ' KB';
    return (bytes / (1024 * 1024)).toFixed(1) + ' MB';
}

function escapeHtml(text) {
    const div = document.createElement('div');
    div.textContent = text;
    return div.innerHTML;
}
