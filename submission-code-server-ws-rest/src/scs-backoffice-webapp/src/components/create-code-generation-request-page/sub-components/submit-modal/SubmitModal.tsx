import React, {useEffect, useState} from 'react';
import {Modal} from 'react-bootstrap';

import {FailedBody, SucceededBody, WaitingBody} from "./sub-components/body";
import {FailedFooter, SucceededFooter} from "./sub-components/footer";


export default function SubmitModal({showParam, handleClose, submitResponse} :any) {

    const [response, setResponse] = useState(undefined as any)
    const delay = (ms : number) => new Promise(res => setTimeout(res, ms));

    useEffect(() => {
        if(submitResponse) {
            submitResponse
                .then((response : any )=>{
                    console.log(response)
                    delay(2000).finally(
                        () => setResponse(response)
                    )
                })
                .catch((e :any) => {
                    setResponse(e)
                })
        }

    }, [submitResponse])


    const ResponseBody = () => {
        if(response && response.isSubmitted) return <SucceededBody>{response.message}</SucceededBody>
        if(response && !response.isSubmitted) return <FailedBody>{response.message}</FailedBody>
        return <div/>
    }

    const Body = () => !response ? <WaitingBody/> : <ResponseBody/>

    const Footer = () => {
        if(response && response.isSubmitted) return <SucceededFooter/>
        if(response && !response.isSubmitted) return <FailedFooter setResponse={setResponse} handleClose={handleClose}/>
        return <div/>
    }


    return (
        <>
            <Modal animation show={showParam}>
                <Modal.Header closeLabel={"close"}>
                    <Modal.Title>Request submitted</Modal.Title>
                </Modal.Header>
                <Body/>
                <Footer/>
            </Modal>
        </>
    );
}