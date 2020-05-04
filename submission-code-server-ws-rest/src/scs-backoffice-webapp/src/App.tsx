import React from 'react';
import {BrowserRouter as Router, Link, Route, Switch,} from 'react-router-dom';
import Header from './components/header';
import MainContainer from "./components/main-container";
import Home from "./components/home";
import About from "./components/about";
import Topics from "./components/topics";

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
                <Link to="/topics">Topics</Link>
              </li>
            </ul>
          </div>
        </Header>

        <MainContainer>

          <Switch>
            <Route path="/about">
              <About/>
            </Route>

            <Route path="/topics">
              <Topics/>
            </Route>

            <Route path="/">
              <Home />
            </Route>

          </Switch>
        </MainContainer>

      </Router>
  );
}
