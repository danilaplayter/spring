/* @MENTEE_POWER (C)2025 */
package ru.mentee.power.domain.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class OpenLibrarySearchResponse {

    @JsonProperty("start")
    private Integer start;

    @JsonProperty("num_found")
    private Integer numFound;

    @JsonProperty("numFoundExact")
    private Boolean numFoundExact;

    @JsonProperty("docs")
    private List<BookDoc> docs;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class BookDoc {

        @JsonProperty("key")
        private String key;

        @JsonProperty("title")
        private String title;

        @JsonProperty("author_name")
        private List<String> authorName;

        @JsonProperty("author_key")
        private List<String> authorKey;

        @JsonProperty("first_publish_year")
        private Integer firstPublishYear;

        @JsonProperty("isbn")
        private List<String> isbn;

        @JsonProperty("publisher")
        private List<String> publisher;

        @JsonProperty("language")
        private List<String> language;

        @JsonProperty("subject")
        private List<String> subject;

        @JsonProperty("cover_i")
        private Long coverId;

        @JsonProperty("edition_count")
        private Integer editionCount;

        @JsonProperty("has_fulltext")
        private Boolean hasFulltext;

        @JsonProperty("public_scan_b")
        private Boolean publicScan;

        @JsonProperty("ia")
        private List<String> internetArchiveIds;

        @JsonProperty("number_of_pages_median")
        private Integer numberOfPages;
    }
}
