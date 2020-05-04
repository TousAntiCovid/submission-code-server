import React from 'react';
import './styles/pagination.scss'

const Pagination = ({elementsPerPage, totalCodes, paginate, pageNumber} : any) => {
    const pageNumbers = [];

    for (let i = 1; i <= Math.ceil(totalCodes / elementsPerPage); i++) {
        pageNumbers.push(i);
    }

    return (
        <nav>
            <ul className='pagination'>
                {pageNumbers.map(number => (
                    <li key={number} className='page-item'>
                        <a onClick={() => paginate(number)} className={`page-link ${number === pageNumber? "table-link-selected": ""}`}>
                            {number}
                        </a>
                    </li>
                ))}
            </ul>
        </nav>
    );
};

export default Pagination;