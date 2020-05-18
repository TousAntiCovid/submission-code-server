import React from 'react';
import {BrowserRouter as Router, Link, Route, Switch} from 'react-router-dom';
import Header from './components/header';
import MainContainer from './components/main-container';
import Home from './components/home';
import About from './components/about';
import SearchCodePage from './components/search-codes-page';
/* The following line can be included in your src/index.js or App.js file */
import './styles/app.sass';
import {Nav, Navbar, NavDropdown} from 'react-bootstrap';
import CreateCodeGenerationRequestPage from './components/create-code-generation-request-page';

export default function App() {
  return (
    <div style={{height: '100vh', width: '100vw'}}>
      <Router>
        <Header>
          <Navbar
            variant="dark"
            bg="dark"
            expand="lg"
          >
            <Navbar.Brand href="/">Submission Code Server</Navbar.Brand>

            <Navbar.Toggle
              aria-controls="basic-navbar-nav"
            />

            <Navbar.Collapse id="basic-navbar-nav">
              <Nav className="mr-auto">
                <Nav.Link>
                  <Link to="/">Home</Link>
                </Nav.Link>


                <NavDropdown title="Codes"
                  id="basic-nav-dropdown"
                >
                  <NavDropdown.Item>
                    <Link to="/codes/search">search</Link>
                  </NavDropdown.Item>

                  <NavDropdown.Divider />

                  <NavDropdown.Item>
                    <Link to="/codes/create">create request</Link>
                  </NavDropdown.Item>

                </NavDropdown>
                <Nav.Link>
                  <Link to="/about">About</Link>
                </Nav.Link>
              </Nav>
            </Navbar.Collapse>

          </Navbar>
        </Header>

        <MainContainer>
          <Switch>
            <Route path="/about">
              <About/>
            </Route>

            <Route path="/codes/search">
              <SearchCodePage
                basePath="/codes/search"
              />

            </Route>

            <Route path="/codes/create">
              <CreateCodeGenerationRequestPage
              />
            </Route>

            <Route path="/">
              <Home />
            </Route>
          </Switch>
        </MainContainer>

      </Router>
    </div>

  );
}
