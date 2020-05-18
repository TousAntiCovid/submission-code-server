import axios from 'axios';

const BASE_URL : String = process.env.REACT_APP_BASE_URL ? process.env.REACT_APP_BASE_URL :  "http://localhost:8080/api/v1/views/"

class SubmissionServerCodeApi {
    static async GET(
        path: string,
        header = {headers: {'Content-Type': 'application/json'}}
    ) {
        console.log("base url is : ", BASE_URL)
        console.log("base url from REACT_APP env is : ", process.env.REACT_APP_BASE_URL)

        const url = new URL(BASE_URL.concat(path)).toString();
        console.log('trying to reach : ' + url);
        return axios.get(url, header);
    }

    static async POST(
        path: string,
        data : any,
        header = {headers: {'Content-Type': 'application/json'}}
    ) {
        console.log("base url is : ", BASE_URL)
        console.log("base url from REACT_APP env is : ", process.env.REACT_APP_BASE_URL)

        const url = new URL(BASE_URL.concat(path)).toString();
        console.log('trying to reach : ' + url);
        return axios.post(url, data, header);
    }
}

export default SubmissionServerCodeApi;
