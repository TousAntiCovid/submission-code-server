package fr.gouv.stopc.submission.code.server.ws.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class ViewLotCodeDetailPageDto {

    /**
     * number of pages for {@link #maxByPage}
     */
    int lastPage;

    /**
     * number of the page displayed started at 1
     */
    int actualPage;

    /**
     * maximum of elements to be displayed in page.
     */
    long maxByPage;

    /**
     * lot identifier
     */
    long lot;


    List<ViewLotCodeDetailDto> codes;
}
