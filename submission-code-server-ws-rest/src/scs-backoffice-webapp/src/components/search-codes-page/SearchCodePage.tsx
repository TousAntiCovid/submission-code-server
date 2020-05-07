import React from "react";
import {Route, Switch, useLocation, useRouteMatch} from "react-router-dom";


import CodeSearchForm from "./sub-components/code-search-form/CodeSearchForm";
import CodeDisplayerTable from "./sub-components/code-displayer-table";


export default function SearchCodePage({basePath}:any)  {
    const match = useRouteMatch();

    return(
        <>
           <CodeSearchForm
               basePath={basePath}
           />

            <Switch>
                <Route path={`${match.path}/:lotIdentifier/:elementsPerPage`}>
                    <CodeDisplayerTable
                    />
                </Route>
                <Route path={match.path}>
                    <h3>Please search a lot...</h3>
                </Route>
            </Switch>
        </>
    );

}
