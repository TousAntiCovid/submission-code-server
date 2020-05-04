import React, { useState, useEffect} from 'react';
import CodeTable from './sub-components/posts';
import Pagination from './sub-components/pagination';
import axios from 'axios';
import {useParams, useRouteMatch} from "react-router-dom";

const Table = () => {
    const { lotIdentifier } = useParams()

    const [codes, setCodes] = useState([]);
    const [totalCodes, setTotalCodes] = useState(0);
    const [loading, setLoading] = useState(false);
    const [currentPage, setCurrentPage] = useState(1);
    const [elementsPerPage, setElementsPerPage] = useState(10);

    const fetchPosts = async () => {
        setLoading(true);
        const res = await axios.get(`http://localhost:8080/api/v1/views/lots/${lotIdentifier}/page/${currentPage}/by/${elementsPerPage}`);
        console.log("20200505" ,res.data)
        setCodes(res.data.codes);
        setTotalCodes(res.data.lastPage * res.data.maxByPage)
        setLoading(false);
    };

    useEffect(() => {
        fetchPosts();
    }, []);

    // Get current codes
    const indexOfLastPost = currentPage * elementsPerPage;
    const indexOfFirstPost = indexOfLastPost - elementsPerPage;
    const currentPosts = codes;

    // Change page
    const paginate =(pageNumber : number) => {
        setCurrentPage(pageNumber);
        fetchPosts();
    }

    return (
        <div className='container mt-5'>
            <CodeTable codes={currentPosts} loading={loading} />
            <Pagination
                elementsPerPage={elementsPerPage}
                totalCodes={totalCodes}
                paginate={paginate}
                pageNumber={currentPage}
            />
        </div>
    );
};

export default Table;