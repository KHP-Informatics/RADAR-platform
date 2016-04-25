var db = require('../db');
var Schema = db.Schema;

var ParticipantSchema = Schema({
    id:{ type: Schema.Types.ObjectId},
    fitbitid: {type: String, required: true},
    dateofbirth: {type: String, required: true},
    name: {type: String, required: true},
    gender: {type: String},
    age: {type: Number},
    fitbittoken: [Schema.Types.Mixed]

});

module.exports = db.model('Participant', ParticipantSchema);
