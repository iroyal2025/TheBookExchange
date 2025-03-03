package edu.famu.thebookexchange.model.Abstracts;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.firebase.database.annotations.Nullable;
import lombok.*;

import lombok.Data;
import lombok.NoArgsConstructor;

@Setter
@Getter
@Data
@NoArgsConstructor
@AllArgsConstructor


public abstract class ABooks {
        private String title;
        private String author;
        private String edition;
        private String ISBN;
        private String condition;
        private String description;
        private double price;
        @JsonProperty("is_digital")
        private boolean isDigital;
        @Nullable
        @JsonProperty("digital_copy_path")
        private String digitalCopyPath;


}


