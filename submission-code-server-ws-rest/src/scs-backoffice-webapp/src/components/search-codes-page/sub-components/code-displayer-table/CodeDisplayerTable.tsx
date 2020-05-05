import React, {useEffect, useState} from 'react';
import CodeTable from './sub-components/code-table';
import TablePagination from './sub-components/pagination-table';
import {useParams} from "react-router-dom";
import {Col, Container, Row} from 'react-bootstrap'

import {ListCodeByLotPageAndElementPerPages} from "../../data-provider/SearchCodePageDataProvider";

const CodeDisplayerTable = () => {

    const { lotIdentifier, elementsPerPage } = useParams()

    const [codes, setCodes] = useState([] as any[]);
    const [numberOfPages, setNumberOfPages] = useState(0 as Number);

    const [loading, setLoading] = useState(false);
    const [currentPage, setCurrentPage] = useState(1);

    const delay = (ms : number) => new Promise(res => setTimeout(res, ms));

    const fetchPosts = () => {
        setLoading(true);

        ListCodeByLotPageAndElementPerPages( {
            lotIdentifier:lotIdentifier,
            currentPage:currentPage,
            elementsPerPage:elementsPerPage,
        }) .then(response => {
            setCodes(response.codes);
            setNumberOfPages(response.numberOfPages)
            delay(1000).then(() => {
                setLoading(false);
            })
        })
    };

    // Reloading when lotIdentifier or elementsPerPage changed.
    useEffect(() => {
        fetchPosts();
    }, [lotIdentifier, elementsPerPage]);

    // Get current codes
    const currentCodes = codes;

    // Change page
    const paginate = (pageNumber : number) => {
        setCurrentPage(pageNumber);
        fetchPosts();
    }

    return (
        <Container>
            <Row>
                <Col>
                    <TablePagination
                        numberOfPages={numberOfPages}
                        paginate={paginate}
                        pageNumber={currentPage}
                    />
                </Col>

            </Row>
            <Row className={"code-table-row"}>
                <Col>
                    <CodeTable
                        codes={currentCodes}
                        loading={loading}
                    />
                </Col>
            </Row>

        </Container>

    );

}

export default CodeDisplayerTable;