import { PolymerElement } from '@polymer/polymer/polymer-element.js';
import 'highcharts';

class HighchartsComponentConnector extends PolymerElement {

    static get is() {
        return 'highcharts-component';
    }

    ready() {
        super.ready();
        this._initHighcharts();
    }

    _initHighcharts() {
        // Initialize Highcharts using the options received from the server-side
        const options = this.options;
        Highcharts.chart(this.$.chart, options);
    }

    setOptions(options) {
        // Set new options for the Highcharts chart
        const chart = Highcharts.charts[0];
        if (chart) {
            chart.update(options);
        }
    }
}

customElements.define(HighchartsComponentConnector.is, HighchartsComponentConnector);

