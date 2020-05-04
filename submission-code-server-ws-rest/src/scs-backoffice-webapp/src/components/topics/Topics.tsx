import React, {useState} from "react";
import {Link, Route, Switch, useHistory, useRouteMatch} from "react-router-dom";
import CodeTable from "./sub-component/codetable";

import Form from 'react-bootstrap/Form';
import Button from 'react-bootstrap/Button';


export default function Topics()  {
    const match = useRouteMatch();
    let history = useHistory();
    const [lotIdentifier, setLotIdentifier] = useState("");

    return <div>
        <h2>Topics</h2>

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
                <CodeTable/>
            </Route>
            <Route path={match.path}>
                <h3>Please select a topic.</h3>
            </Route>
        </Switch>


    </div>;

}
