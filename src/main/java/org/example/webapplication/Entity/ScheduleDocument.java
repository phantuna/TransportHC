package org.example.webapplication.Entity;


import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@SQLDelete(sql = "UPDATE schedule_document SET deleted = 1, deleted_at = NOW() WHERE id = ?")
@SQLRestriction("deleted = 0")
public class ScheduleDocument extends Base {

    @ManyToOne
    @JoinColumn(name = "schedule_id")
    private Schedule schedule;

    private String fileName;
    private String fileUrl;
    private String fileType;
    private long fileSize;

}
