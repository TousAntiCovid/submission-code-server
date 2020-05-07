import React, {useState} from 'react';
import {Button, Card, Col, Container, Form} from 'react-bootstrap';
import * as Helper from "./tools/CreateCodeGenerationRequestPageHelper"
import {submitCodeGenerationRequest} from "./data-provider/CreateCodeGenerationRequestPageDataProvider";
import SubmitModal from "./sub-components/submit-modal";

export default function CreateCodeGenerationRequestPage() {
    const [startdate, setStartDate] = useState(new Date())
    const [enddate, setEndDate] = useState(new Date())
    const [codePerDay, setCodePerDay] = useState(100);
    const [submitResponse, setSubmitResponse] = useState();

    const [show, setShow] = useState(false);
    const handleClose = () => setShow(false);
    const handleShow = () => setShow(true);

    const formValues = () => {
        return  {
            from : startdate,
            to : enddate,
            codePerDay : codePerDay
        }
    }

    const handleSubmit = (event : any) => {
        handleShow()
        setSubmitResponse(submitCodeGenerationRequest(
            formValues()
        ))
    };


    return <>
        <SubmitModal
            showParam={show}
            handleClose={handleClose}
            formValues={formValues}
            submitResponse={submitResponse}
        />

        <Container fluid={"sm"}>

            <Card
                bg={"light"}>
                <Card.Header>
                    <Card.Title>Code generation request</Card.Title>
                </Card.Header>

                <Card.Body>

                    <Card.Text>
                        <Form>
                            <Form.Row>
                                <Form.Group as={Col} controlId="formGridCity">
                                    <Form.Label>From :</Form.Label>
                                    <Form.Control type={"date"}
                                                  value={Helper.parseDate(startdate)}
                                                  min={Helper.parseDate()}
                                                  onChange={(e) => {
                                                      const newStartDate = e.target.value ? new Date(e.target.value) : Helper.truncateToDays()
                                                      setStartDate(newStartDate)
                                                      if(newStartDate > enddate) {
                                                          setEndDate(newStartDate)
                                                      }
                                                  }}
                                    />
                                </Form.Group>

                                <Form.Group as={Col} controlId="formGridCity">
                                    <Form.Label>To :</Form.Label>
                                    <Form.Control type={"date"}
                                                  value={Helper.parseDate(enddate)}
                                                  min={Helper.parseDate(startdate)}
                                                  onChange={(e) => {
                                                      const newEtartDate = e.target.value ? new Date(e.target.value) : startdate
                                                      setEndDate(newEtartDate)
                                                  }}
                                    />
                                </Form.Group>
                            </Form.Row>

                            <Form.Row>
                                <Form.Group as={Col} controlId="formGridState">
                                    <Form.Label>Code per days : {codePerDay} </Form.Label>
                                    <Form.Control type="range" custom
                                                  min={0}
                                                  step={500}
                                                  max={10000}
                                                  value={codePerDay}
                                                  onChange={(e) => {
                                                      setCodePerDay(parseInt(e.target.value))
                                                  }}
                                    />
                                </Form.Group>
                            </Form.Row>

                            <Form.Row>
                                <Form.Group as={Col} controlId="formGridCity">
                                    <Form.Label>Total of days</Form.Label>
                                    <Form.Control disabled
                                                  value={Helper.daysBetween(startdate, enddate) +1}
                                    />
                                </Form.Group>

                                <Form.Group as={Col} controlId="formGridCity">
                                    <Form.Label>Total codes</Form.Label>
                                    <Form.Control disabled
                                                  value={
                                                      ((Helper.daysBetween(startdate, enddate) +1) * codePerDay)
                                                          .toLocaleString()
                                                  }
                                    />
                                </Form.Group>
                            </Form.Row>
                            <Form.Row>
                                <Form.Group as={Col} md={11} controlId="formGridEmpty"/>

                                <Form.Group as={Col} md={1} controlId="formGridCity">
                                    <Button variant="outline-primary" onClick={handleSubmit}>
                                        Submit
                                    </Button>
                                </Form.Group>

                            </Form.Row>

                        </Form>
                    </Card.Text>
                </Card.Body>
            </Card>
        </Container>

    </>
}