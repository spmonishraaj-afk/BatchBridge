package com.batchbridge.controller;

import com.batchbridge.model.Note;
import com.batchbridge.service.FileStorageService;
import com.batchbridge.service.NoteService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/notes")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class NoteController {

    private final NoteService noteService;
    private final FileStorageService fileStorageService;

    @GetMapping
    public ResponseEntity<List<Note>> getAllNotes() {
        return ResponseEntity.ok(noteService.getAllNotes());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Note> getNoteById(@PathVariable Long id) {
        return ResponseEntity.ok(noteService.getNoteById(id));
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Note> uploadNote(
            @RequestParam("title") String title,
            @RequestParam(value = "description", required = false) String description,
            @RequestParam("subject") String subject,
            @RequestParam("semester") String semester,
            @RequestParam("department") String department,
            @RequestParam("uploadedBy") String uploadedBy,
            @RequestParam(value = "file", required = false) MultipartFile file) {

        Note note = new Note();
        note.setTitle(title);
        note.setDescription(description);
        note.setSubject(subject);
        note.setSemester(semester);
        note.setDepartment(department);
        note.setUploadedBy(uploadedBy);

        Note savedNote = noteService.uploadNote(note, file);
        return ResponseEntity.ok(savedNote);
    }

    @PutMapping(value = "/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Note> updateNote(
            @PathVariable Long id,
            @RequestParam("title") String title,
            @RequestParam(value = "description", required = false) String description,
            @RequestParam("subject") String subject,
            @RequestParam("semester") String semester,
            @RequestParam("department") String department,
            @RequestParam("uploadedBy") String uploadedBy,
            @RequestParam(value = "file", required = false) MultipartFile file) {

        Note note = new Note();
        note.setTitle(title);
        note.setDescription(description);
        note.setSubject(subject);
        note.setSemester(semester);
        note.setDepartment(department);
        note.setUploadedBy(uploadedBy);

        Note updatedNote = noteService.updateNote(id, note, file);
        return ResponseEntity.ok(updatedNote);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteNote(@PathVariable Long id) {
        noteService.deleteNote(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/search")
    public ResponseEntity<List<Note>> searchNotes(
            @RequestParam(required = false) String subject,
            @RequestParam(required = false) String semester,
            @RequestParam(required = false) String department) {
        return ResponseEntity.ok(noteService.searchNotes(subject, semester, department));
    }

    @GetMapping("/search/keyword")
    public ResponseEntity<List<Note>> searchByKeyword(@RequestParam String keyword) {
        return ResponseEntity.ok(noteService.fullTextSearch(keyword));
    }

    @GetMapping("/download/{id}")
    public ResponseEntity<Resource> downloadFile(@PathVariable Long id, HttpServletRequest request) {
        Note note = noteService.getNoteById(id);
        Resource resource = fileStorageService.loadFileAsResource(note.getFileName());

        String contentType = note.getFileType();
        if (contentType == null) {
            contentType = "application/octet-stream";
        }

        String downloadName = note.getTitle().replaceAll("[^a-zA-Z0-9]", "_");
        String extension = note.getFileName().substring(note.getFileName().lastIndexOf('.'));

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(contentType))
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"" + downloadName + extension + "\"")
                .body(resource);
    }
}
