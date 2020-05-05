import React from 'react';
import Table from 'react-bootstrap/Table'

import "./styles/code-table.sass"

import Loading from "../../../../../loading";

const CodeTable = ({codes , loading} : any) => {

    if (loading === true ) {
        return <Loading/>;
    } else {
        return (
            <div className={"scroll"}>
            <Table  bordered>
                <thead>
                <tr>
                    <th>
                        Table Row
                    </th>
                    <th>
                        Code
                    </th>
                </tr>
                </thead>
                <tbody >
                {codes.map((code : {code:string}, i : number)=> (
                    <tr>
                        <td key={i+1}>
                            {i+1}
                        </td>
                        <td key={code.code}>
                            {code.code}
                        </td>
                    </tr>
                ))}

                </tbody>
            </Table>
            </div>
        );
    }
};

export default CodeTable;