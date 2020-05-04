import React from 'react';
import Table from 'react-bootstrap/Table'

const CodeTable = ({codes, loading } : any) => {
    if (loading) {
        return <h2>Loading...</h2>;
    }

    return (
        <Table>
            <thead>
            <tr>
                <th>
                    CODE
                </th>
            </tr>
            </thead>
            <tbody>
            {codes.map((code : {code:string} )=> (
                <tr>
                    <td key={code.code}>
                        {code.code}
                    </td>
                </tr>
            ))}

            </tbody>
        </Table>

    );
};

export default CodeTable;