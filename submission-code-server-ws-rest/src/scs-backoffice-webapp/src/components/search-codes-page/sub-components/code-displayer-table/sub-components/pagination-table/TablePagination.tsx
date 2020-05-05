import React from 'react';
import './styles/pagination.sass'

import {Pagination} from 'react-bootstrap'

const TablePagination = ({numberOfPages, paginate, pageNumber} : any) => {
    const pageNumbers = [];

    for (let i = 1; i <= numberOfPages; i++) {
        pageNumbers.push(i);
    }

    return (
        <>
            <Pagination className="table-pagination">
                {pageNumbers.map(number => (
                    <Pagination.Item
                        key={number}
                        onClick={() => paginate(number)}
                        active={number === pageNumber}
                    >
                        {number}
                    </Pagination.Item>
                ))}
            </Pagination>
        </>
    );
};

export default TablePagination;