var db = require('../db');
var Schema = db.Schema;

var DataSchema = Schema({
    id:{ type: Schema.Types.ObjectId},
    fitbitid: {type: String, required: true},
    date: {type: String, required: true},
    type: {type: String, required: true},
    data: [Schema.Types.Mixed]

});

module.exports = db.model('Data', DataSchema);
