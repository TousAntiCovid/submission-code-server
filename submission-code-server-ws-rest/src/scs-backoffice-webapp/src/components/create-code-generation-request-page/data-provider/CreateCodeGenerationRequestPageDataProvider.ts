import SubmissionServerCodeApi from "../../../toolbox/SubmissionServerCodeApi";


export type ResponseSubmitCodeGenerationRequestType = {
    isSubmitted : boolean,
    message: string,
    data: string,
}

export type RequestSubmitCodeGenerationRequestType = {
    from : Date,
    to : Date,
    dailyAmount : Number
}

export function submitCodeGenerationRequest(
    requestParams: RequestSubmitCodeGenerationRequestType
) : Promise<ResponseSubmitCodeGenerationRequestType>
{
    return SubmissionServerCodeApi.POST(
        `codes/generation/request`,
        requestParams
    ).then(response => {
        console.log("response from  codes/generation/request ", response.data)
        return {
            isSubmitted : response.data.isSubmitted as boolean,
            message : response.data.message as string,
            data : response.data.baos as string
        };

    })
        .catch(err => {
            console.error(err);
            return {
                isSubmitted : false,
                message: err.toString() as string,
                data: {} as string
            }
        });
}