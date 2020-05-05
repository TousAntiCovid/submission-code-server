import SubmissionServerCodeApi from "../../../toolbox/SubmissionServerCodeApi";


export type ResponseCodeByLotPageAndElementPerPagesType = {
    codes : any[],
    numberOfPages: Number,
}

export type RequestCodeByLotPageAndElementPerPagesType = {
    lotIdentifier : Number,
    currentPage : Number,
    elementsPerPage : Number
}

export function ListCodeByLotPageAndElementPerPages(
    requestParams: RequestCodeByLotPageAndElementPerPagesType
) : Promise<ResponseCodeByLotPageAndElementPerPagesType>
{
    return SubmissionServerCodeApi.GET(
        `lots/${requestParams.lotIdentifier}/page/${requestParams.currentPage}/by/${requestParams.elementsPerPage}`
    ).then(response => {
        return {
            codes : response.data.codes,
            numberOfPages : parseInt(response.data.lastPage)
        };

    })
        .catch(err => {
            console.error(err);
            return {} as ResponseCodeByLotPageAndElementPerPagesType
        });
}