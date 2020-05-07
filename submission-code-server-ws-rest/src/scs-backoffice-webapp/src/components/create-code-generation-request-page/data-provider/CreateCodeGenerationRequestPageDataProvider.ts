import SubmissionServerCodeApi from "../../../toolbox/SubmissionServerCodeApi";


export type ResponseSubmitCodeGenerationRequestType = {
    isSubmitted : boolean,
    message: string,
}

export type RequestSubmitCodeGenerationRequestType = {
    from : Date,
    to : Date,
    codePerDay : Number
}

export function submitCodeGenerationRequest(
    requestParams: RequestSubmitCodeGenerationRequestType
) : Promise<ResponseSubmitCodeGenerationRequestType>
{
    return SubmissionServerCodeApi.POST(
        `codes/generation/request`,
        requestParams
    ).then(response => {
        return {
            isSubmitted : response.data.isSubmitted,
            message : response.data.message
        };

    })
        .catch(err => {
            console.error(err);
            return {
                isSubmitted : false,
                message: err.toString()
            }
        });
}