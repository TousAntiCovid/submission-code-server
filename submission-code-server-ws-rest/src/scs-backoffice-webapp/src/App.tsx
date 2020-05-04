import React from 'react';
import {BrowserRouter as Router, Link, Route, Switch,} from 'react-router-dom';
import Header from './components/header';
import MainContainer from "./components/main-container";
import Home from "./components/home";
import About from "./components/about";
import CodeListPage from "./components/topics";
/* The following line can be included in your src/index.js or App.js file */
import './style/App.scss';
import Table from "./components/table";

export default function App() {

    return (
        <Router>
            <Header>
                <div>
                    <ul>
                        <li>
                            <Link to="/">Home</Link>
                        </li>
                        <li>
                            <Link to="/about">About</Link>
                        </li>
                        <li>
                            <Link to="/codes">Codes</Link>
                        </li>
                    </ul>
                </div>
            </Header>

            <MainContainer>
                <Switch>
                    <Route path="/about">
                        <About/>
                    </Route>

                    <Route path="/codes">
                        <CodeListPage/>
                    </Route>

                    <Route path="/">
                        <Home />
                    </Route>
                </Switch>
            </MainContainer>

        </Router>
    );
}
