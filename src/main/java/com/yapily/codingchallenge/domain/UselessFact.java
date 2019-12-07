package com.yapily.codingchallenge.domain;


import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.UUID;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Document
@Builder
public class UselessFact {
    @Id
    @JsonIgnore
    private String mongoId;
    @ApiModelProperty(notes = "The fact")
    private String text;
    @JsonProperty("id")
    @ApiModelProperty(name = "Id", required = true, notes = "UUID reference for a fact")
    private UUID factId;
    @JsonProperty("source_url")
    @ApiModelProperty(notes = "The original fact source")
    private String sourceUrl;
    @ApiModelProperty(notes = "The language of the fact")
    private String language;
    @ApiModelProperty(notes = "Permanent link to the fact")
    private String permalink;
    @ApiModelProperty(notes = "Denotes if the fact has been translated")
    private boolean translated;
}
