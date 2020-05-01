import React, {Component} from 'react';

export default class MainContainer extends Component {
  render(): React.ReactElement {
    return <>{this.props.children}</>;
    }
}
