import {Spinner} from "react-bootstrap";
import React from "react";

import "./styles/loading.sass"

const Loading = () => (
    <>
        <div>
            <div className={"fixed-top loading"}>
                <Spinner  style={{
                    display: "block",
                    margin: "auto",
                }}
                          animation="grow"
                          variant="primary"
                />
            </div>
        </div>
    </>
);

export default Loading