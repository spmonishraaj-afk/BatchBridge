package com.batchbridge.service;

import com.batchbridge.exception.ResourceNotFoundException;
import com.batchbridge.model.Note;
import com.batchbridge.repository.NoteRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Service
@RequiredArgsConstructor
public class NoteService {

    private final NoteRepository noteRepository;
    private final FileStorageService fileStorageService;

    public List<Note> getAllNotes() {
        return noteRepository.findAll();
    }

    public Note getNoteById(Long id) {
        return noteRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Note not found with id: " + id));
    }

    public Note uploadNote(Note note, MultipartFile file) {
        if (file != null && !file.isEmpty()) {
            String fileName = fileStorageService.storeFile(file);
            note.setFileName(fileName);
            note.setFileType(file.getContentType());
            note.setFilePath(fileName);
            note.setFileSize(file.getSize());
        }
        return noteRepository.save(note);
    }

    public Note updateNote(Long id, Note updatedNote, MultipartFile file) {
        Note existingNote = getNoteById(id);

        existingNote.setTitle(updatedNote.getTitle());
        existingNote.setDescription(updatedNote.getDescription());
        existingNote.setSubject(updatedNote.getSubject());
        existingNote.setSemester(updatedNote.getSemester());
        existingNote.setDepartment(updatedNote.getDepartment());
        existingNote.setUploadedBy(updatedNote.getUploadedBy());

        if (file != null && !file.isEmpty()) {
            if (existingNote.getFileName() != null) {
                fileStorageService.deleteFile(existingNote.getFileName());
            }
            String fileName = fileStorageService.storeFile(file);
            existingNote.setFileName(fileName);
            existingNote.setFileType(file.getContentType());
            existingNote.setFilePath(fileName);
            existingNote.setFileSize(file.getSize());
        }

        return noteRepository.save(existingNote);
    }

    public void deleteNote(Long id) {
        Note note = getNoteById(id);
        if (note.getFileName() != null) {
            fileStorageService.deleteFile(note.getFileName());
        }
        noteRepository.delete(note);
    }

    public List<Note> searchNotes(String subject, String semester, String department) {
        return noteRepository.searchNotes(subject, semester, department);
    }

    public List<Note> fullTextSearch(String keyword) {
        return noteRepository.fullTextSearch(keyword);
    }
}
