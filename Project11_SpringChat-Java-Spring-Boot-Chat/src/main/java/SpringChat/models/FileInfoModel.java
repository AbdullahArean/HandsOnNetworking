package SpringChat.models;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.Column;
import javax.persistence.Entity;
import java.util.UUID;


@Entity
@NoArgsConstructor
@Getter
@Setter
public class FileInfoModel extends BaseModel {

    @Column(length = 200)
    private String name;

    @Column
    private Long length;

    @Column
    private UUID fileDataId;

    @Column
    private UUID imgPrevFileDataId;
}
