import React, {useState} from "react";
import {Link, Route, Switch, useHistory, useRouteMatch} from "react-router-dom";
import CodeTable from "./sub-component/codetable";

import Form from 'react-bootstrap/Form';
import Button from 'react-bootstrap/Button';
import Table from "../table";


export default function CodeListPage()  {
    const match = useRouteMatch();
    let history = useHistory();
    const [lotIdentifier, setLotIdentifier] = useState("");

    return <div>
        <h2>CODES</h2>

        <Form >
            <Form.Group>
                <Form.Label>Lot identifier</Form.Label>
                <Form.Control type="text"
                              value={lotIdentifier}
                              onChange={(e) => setLotIdentifier(e.target.value)}
                />
            </Form.Group>

            <Link to={`${match.url}/${lotIdentifier}`}>
                <Button>search</Button>
            </Link>
        </Form>

        <Switch>
            <Route path={`${match.path}/:lotIdentifier`}>
                <Table/>
            </Route>
            <Route path={match.path}>
                <h3>Please select a topic.</h3>
            </Route>
        </Switch>


    </div>;

}
