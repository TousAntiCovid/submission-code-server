import axios from 'axios';

const BASE_URL : String = "http://localhost:8080/api/v1/views/"

class SubmissionServerCodeApi {
    static async GET(
        path: string,
        header = {headers: {'Content-Type': 'application/json'}}
    ) {
        const url = new URL(BASE_URL.concat(path)).toString();
        console.log('trying to reach : ' + url);
        return axios.get(url, header);
    }
}

export default SubmissionServerCodeApi;
