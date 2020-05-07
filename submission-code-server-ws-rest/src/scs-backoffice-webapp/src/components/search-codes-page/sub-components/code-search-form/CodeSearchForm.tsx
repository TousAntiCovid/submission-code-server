import React, {useEffect, useState} from "react";
import {Link, useLocation, useRouteMatch} from "react-router-dom";

import {Button, Form, FormControl, InputGroup, Navbar} from 'react-bootstrap';
import {parseBasePath} from "./tools/CodeSearchFormHelper";


export default function CodeSearchForm({basePath}:any)  {
    const match = useRouteMatch();
    let location = useLocation();

    const [lotIdentifier, setLotIdentifier] = useState("");
    const [elementsPerPage, setElementByPages] = useState(10);
    useEffect(() => {
        parseBasePath(location.pathname, basePath, setLotIdentifier, setElementByPages);
        }, [location]
    )

    return(
        <>
            <Navbar className="bg-light justify-content-between">
                <Form inline>
                    <InputGroup>
                        <InputGroup.Prepend>
                            <InputGroup.Text id="basic-addon1">n°</InputGroup.Text>
                        </InputGroup.Prepend>
                        <FormControl
                            placeholder="lot"
                            aria-label="lot"
                            aria-describedby="basic-addon1"
                            value={lotIdentifier}
                            onChange={(e) => setLotIdentifier(e.target.value)}
                        />
                    </InputGroup>

                    <InputGroup>
                        <InputGroup.Prepend>
                            <InputGroup.Text id="basic-addon2">by</InputGroup.Text>
                        </InputGroup.Prepend>
                        <FormControl
                            as={"select"}
                            placeholder="n° lot"
                            aria-label="n° lot"
                            aria-describedby="basic-addon2"
                            value={elementsPerPage}
                            onChange={(e) => setElementByPages(parseInt(e.target.value))}
                        >
                            <option>10</option>
                            <option>50</option>
                            <option>100</option>
                            <option>200</option>
                            <option>500</option>
                        </FormControl>
                    </InputGroup>
                </Form>

                <Form inline>
                    <Link to={`${match.url}/${lotIdentifier}/${elementsPerPage}`}>
                        <Button variant="outline-success">Search</Button>
                    </Link>
                </Form>

            </Navbar>
        </>
    );

}
