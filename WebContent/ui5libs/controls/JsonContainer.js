sap.ui.define([
	'ui5libs/controls/ModelContainer'
], function(ModelContainer) {
  return ModelContainer.extend("ui5libs.controls.JsonContainer", {
		metadata : {
			properties: {
			}
		},
		createNewModel : function() {
			var url = this.getUrl();
			var oModel = new sap.ui.model.json.JSONModel(); 
			if (this.getSizeLimit()) {
				oModel.setSizeLimit(this.getSizeLimit());
			}
			if (url) {
				oModel.loadData(sap.ui.require.toUrl(url));
    		}
			return oModel;
		}
	});
});
