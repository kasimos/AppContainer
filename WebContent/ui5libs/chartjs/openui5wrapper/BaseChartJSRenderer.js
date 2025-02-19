sap.ui.define(['jquery.sap.global'],
	function(jQuery) {
	"use strict";


	/**
	* BaseChartJS renderer.
	* @static
	* @namespace
	*/
	var BaseChartJSRenderer = {};
	
	/**
	 * Renders the HTML for the given control, using the provided {@link sap.ui.core.RenderManager}.
	 *
	 * @param {sap.ui.core.RenderManager} oRm The RenderManager that can be used for writing to the render output buffer.
	 * @param {sap.ui.core.Control} oControl An object representation of the control that should be rendered.
	 */
	BaseChartJSRenderer.render = function(oRM, oControl) {
		oRM.write("<div");
		oRM.writeControlData(oControl);
		oRM.addClass("opeui5-base-chartjs");
		this.addOuterClasses(oRM, oControl);
		oRM.writeClasses();
		
		if (oControl.getHeight() !== undefined && oControl.getHeight() !== null) {
			oRM.addStyle("height", oControl.getHeight());
		}
		if (oControl.getWidth() !== undefined && oControl.getWidth() !== null) {
			oRM.addStyle("width", oControl.getWidth());
		}
		oRM.addStyle("position", "relative");
		oRM.writeStyles();
		oRM.write("><canvas id='" + oControl.getId() + "-canvas'>");
		
		oRM.write("</canvas></div>");
	};
	
	/**
	 * This method is reserved for derived classes to add extra classes for chart container.
	 *
	 * @param {sap.ui.core.RenderManager} oRm The RenderManager that can be used for writing to the render output buffer.
	 * @param {sap.ui.core.Control} oControl An object representation of the control that should be rendered.
	 */
	BaseChartJSRenderer.addOuterClasses = function(oRm, oControl) {};


	return BaseChartJSRenderer;

}, /* bExport= */ true);
