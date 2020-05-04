import axios from 'axios';

class SubmissionServerCodeApi {

    static async GET(
        path: string,
        header = {headers: {'Content-Type': 'application/json'}}
    ) {
        const url = new URL( path).toString();
        console.log('trying to reach : ' + url);
        return axios.get(url, header);
    }
}

export default SubmissionServerCodeApi;
