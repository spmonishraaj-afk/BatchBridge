package com.batchbridge.repository;

import com.batchbridge.model.Note;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface NoteRepository extends JpaRepository<Note, Long> {

    List<Note> findBySubjectContainingIgnoreCase(String subject);
    List<Note> findBySemesterContainingIgnoreCase(String semester);
    List<Note> findByDepartmentContainingIgnoreCase(String department);
    List<Note> findByTitleContainingIgnoreCase(String title);

    @Query("SELECT n FROM Note n WHERE " +
           "(:subject IS NULL OR LOWER(n.subject) LIKE LOWER(CONCAT('%', :subject, '%'))) AND " +
           "(:semester IS NULL OR LOWER(n.semester) LIKE LOWER(CONCAT('%', :semester, '%'))) AND " +
           "(:department IS NULL OR LOWER(n.department) LIKE LOWER(CONCAT('%', :department, '%')))")
    List<Note> searchNotes(@Param("subject") String subject,
                            @Param("semester") String semester,
                            @Param("department") String department);

    @Query("SELECT n FROM Note n WHERE " +
           "LOWER(n.title) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(n.subject) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(n.description) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    List<Note> fullTextSearch(@Param("keyword") String keyword);
}
