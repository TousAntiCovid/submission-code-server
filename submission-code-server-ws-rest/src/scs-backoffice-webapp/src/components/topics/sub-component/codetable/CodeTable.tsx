import React, {useState} from "react";
import {useParams} from "react-router-dom";
import SubmissionServerCodeApi from "../../../../toolbox/SubmissionServerCodeApi";
import Button from 'react-bootstrap/Button';
import Table from 'react-bootstrap/Table';

export default function CodeTable() {
    const [lotInformation, setLotInformation] = useState();

    const { lotIdentifier } = useParams()

    //retrieveLotInformation(lotIdentifier, setLotInformation)
    return <>
        <h3>Looking for lot identifier : {lotIdentifier}</h3>
        <div>
            <Button onClick={() => {
                SubmissionServerCodeApi.GET(`http://localhost:8080/api/v1/views/lots/${lotIdentifier}/information`)
                    .then((response: { data: any; }) => {
                        setLotInformation(response.data)
                    })
            }}>show</Button>
            {JSON.stringify(lotInformation)}
        </div>

        <Table>
            <thead>
            <tr>
                <th>Colomn 1</th>
                <th>Colomn 2</th>
                <th>Colomn 3</th>
            </tr>
            </thead>
            <tbody>
            <tr>
                <td>Value 1</td>
                <td>Value 2</td>
                <td>Value 3 </td>
            </tr>

            <tr>
                <td >

                </td>
            </tr>
            </tbody>
        </Table>

    </>;
}


