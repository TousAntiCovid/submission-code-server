import React, {Component} from "react";

export default class Header extends Component {

    render(): React.ReactElement<any, string | React.JSXElementConstructor<any>> | string | number | {} | React.ReactNodeArray | React.ReactPortal | boolean | null | undefined {
        return <h1>{this.props.children}</h1>;
    }
}
