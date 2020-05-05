import React from "react";
import {Route, Switch, useLocation, useRouteMatch} from "react-router-dom";


import CodeSearchForm from "./sub-components/code-search-form/CodeSearchForm";
import CodeDisplayerTable from "./sub-components/code-displayer-table";


export default function SearchCodePage()  {
    const match = useRouteMatch();
    let location = useLocation();

    let initialLot = "";
    let initialElementPerpage = 10;

    if (location.pathname.startsWith("/codes")) {
        const splittedPath = location.pathname.split("/")
        if(splittedPath.length > 2) initialLot = splittedPath[2]
        if(splittedPath.length >= 3) initialElementPerpage = parseInt(splittedPath[3])
    }


    return(
        <>
           <CodeSearchForm/>

            <Switch>
                <Route path={`${match.path}/:lotIdentifier/:elementsPerPage`}>
                    <CodeDisplayerTable/>
                </Route>
                <Route path={match.path}>
                    <h3>Please search a lot...</h3>
                </Route>
            </Switch>
        </>
    );

}
