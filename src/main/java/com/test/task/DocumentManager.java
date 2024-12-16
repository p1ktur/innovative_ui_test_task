package com.test.task;

import lombok.Builder;
import lombok.Data;

import java.time.Instant;
import java.util.*;
import java.util.function.Predicate;

/**
 * For implement this task focus on clear code, and make this solution as simple readable as possible
 * Don't worry about performance, concurrency, etc
 * You can use in Memory collection for sore data
 * <p>
 * Please, don't change class name, and signature for methods save, search, findById
 * Implementations should be in a single class
 * This class could be auto tested
 */
public class DocumentManager {

    private final HashMap<String, Integer> storedDocumentsIndexesById = new HashMap<>();
    private final ArrayList<Document> storedDocuments = new ArrayList<>();

    /**
     * Implementation of this method should upsert the document to your storage
     * And generate unique id if it does not exist, don't change [created] field
     *
     * @param document - document content and author data
     * @return saved document
     */
    public Document save(Document document) {
        if (document.getCreated() == null) {
            document.setCreated(Instant.now());
        }

        if (storedDocumentsIndexesById.containsKey(document.getId())) {
            int index = storedDocumentsIndexesById.get(document.getId());
            storedDocuments.set(index, document);
        } else {
            String newId = UUID.randomUUID().toString();
            document.setId(newId);

            int index = storedDocuments.size();
            storedDocumentsIndexesById.put(newId, index);

            storedDocuments.add(document);
        }
        return document;
    }

    /**
     * Implementation this method should find documents which match with request
     *
     * @param request - search request, each field could be null
     * @return list matched documents
     */
    public List<Document> search(SearchRequest request) {
        Predicate<? super Document> docFilter =doc -> isDocumentRequested(request, doc);

        return storedDocuments.stream().filter(docFilter).toList();
    }

    /**
     * Implementation this method should find document by id
     *
     * @param id - document id
     * @return optional document
     */
    public Optional<Document> findById(String id) {
        if (!storedDocumentsIndexesById.containsKey(id)) return Optional.empty();

        int index = storedDocumentsIndexesById.get(id);
        return Optional.ofNullable(storedDocuments.get(index));
    }

    private Boolean isDocumentRequested(SearchRequest request, Document document) {
        if (request.titlePrefixes != null && !request.titlePrefixes.isEmpty()) {
            boolean prefixesMatch = request.titlePrefixes.stream().anyMatch(pref -> document.getTitle().startsWith(pref));
            if (!prefixesMatch) return false;
        }

        if (request.containsContents != null && !request.containsContents.isEmpty()) {
            boolean contentsMatch = request.containsContents.stream().allMatch(con -> document.getContent().contains(con));
            if (!contentsMatch) return false;
        }

        if (request.authorIds != null && !request.authorIds.isEmpty()) {
            boolean authorMatch = request.authorIds.stream().allMatch(id -> Objects.equals(document.author.id, id));
            if (!authorMatch) return false;
        }

        if (request.createdFrom != null && document.created.isBefore(request.createdFrom)) return false;
        if (request.createdTo != null && document.created.isAfter(request.createdTo)) return false;

        return true;
    }

    @Data
    @Builder
    public static class SearchRequest {
        private List<String> titlePrefixes;
        private List<String> containsContents;
        private List<String> authorIds;
        private Instant createdFrom;
        private Instant createdTo;
    }

    @Data
    @Builder
    public static class Document {
        private String id;
        private String title;
        private String content;
        private Author author;
        private Instant created;
    }

    @Data
    @Builder
    public static class Author {
        private String id;
        private String name;
    }
}