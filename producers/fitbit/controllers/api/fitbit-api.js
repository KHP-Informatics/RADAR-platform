var router = require('express').Router();
var moment = require('moment');
var FitbitRequest = require('./requests/fitbit-requests');
var Participant = require('../../models/ParticipantModel')
var Data = require('../../models/DataModel')
var config = require('../../config');

router.get('/profile', function(req, res){

    var token = JSON.parse(process.env.TOKEN);
    var fitbit = new FitbitRequest({
        token: token
    });


    var callback = function(error, response, body){
        if(error) console.error(error);
        if (!error && response.statusCode == 200) {
            var data = JSON.parse(body);
            console.log("Data received");

            Participant.findOne({'fitbitid': data.user.encodedId}, 'name', function(err, participant){
                if(err){res.send(err);}
                if(!participant){
                    var participant = new Participant({
                        fitbitid: data.user.encodedId,
                        name: data.user.fullName,
                        age: data.user.age,
                        gender: data.user.gender,
                        dateofbirth: data.user.dateOfBirth,
                        fitbittoken: token
                    });

                    participant.save(function(err){
                        if(err) res.send(err);
                        else res.send({fitbitCall: "Profile",
                                        success: true});
                    });
                } else {
                    res.send({fitbitCall: "Profile",
                                    success: false,
                                    message: participant.name + " already exists."});;
                }
            });
        }
    }
    fitbit.getData('profile', 'YYYY-MM-DD', callback);
});


router.get('/sleep', function(req, res){

    // get json from req to obtain date range
    // curl command:
    // curl -X GET -H "Content-type: application/json" -d '{"date":{"begin":"yyyy-mm-dd", "end":"yyyy-mm-dd"}}' http://localhost:3000/api/fitbit/sleep
    // ERROR HANDLING HERE
    var date_begin = moment(req.body.date.begin);
    var date_end = moment(req.body.date.end);

    var token = JSON.parse(process.env.TOKEN);
    var fitbit = new FitbitRequest({
        token: token
    });

    var callback = function(error, response, body){
        if(error) console.error(error);
        if (!error && response.statusCode == 200) {
            var data = JSON.parse(body);

            if(data.sleep[0]){
                var first_recorded_sleep = data.sleep[0];
                console.log("Sleep - received:", first_recorded_sleep.dateOfSleep);

                Data.findOne({'fitbitid': token.user_id,
                                'date':first_recorded_sleep.dateOfSleep,
                                'type':'sleep'},
                                'fitbitid date', function(err, data_mongo){
                    if(err) console.log(err);
                    if(!data_mongo){
                        var data_mongo = new Data({
                            fitbitid: token.user_id,
                            date: first_recorded_sleep.dateOfSleep.toString(),
                            type:'sleep',
                            data: data.sleep
                        });

                        data_mongo.save(function(err){
                            if(err) console.log(err);
                        });
                    } else {
                        console.log({fitbitCall: "Sleep",
                                        success: false,
                                        message: data_mongo.fitbitid + " on " +
                                                    data_mongo.date + " already exists."});
                    }
                });
            }
        }
    }

    for(var m = date_begin; m.isBefore(date_end); m.add(1, 'days')){
        console.log("Sleep - requesting:",m.format('YYYY-MM-DD'));
        fitbit.getData('sleep', m.format('YYYY-MM-DD'), callback);
    }

    res.send({fitbitCall: "Sleep", success: true});
});

router.get('/heart', function(req, res){
    // get json from req to obtain date range
    // curl command:
    // curl -X GET -H "Content-type: application/json" -d '{"date":{"begin":"yyyy-mm-dd", "end":"yyyy-mm-dd"}}' http://localhost:3000/api/fitbit/heart
    // ERROR HANDLING HERE
    var date_begin = moment(req.body.date.begin);
    var date_end = moment(req.body.date.end);

    var token = JSON.parse(process.env.TOKEN);
    var fitbit = new FitbitRequest({
        token: token
    });

    var callback = function(error, response, body){
        if(error) console.error(error);
        if (!error && response.statusCode == 200) {
            var data = JSON.parse(body);

            if(data['activities-heart'][0]){
                var heartSummary = data['activities-heart'][0];
                console.log("Heart - received:", heartSummary.dateTime);

                Data.findOne({'fitbitid': token.user_id,
                                'date':heartSummary.dateTime,
                                'type':'heart'},
                                'fitbitid date', function(err, data_mongo){
                    if(err) console.log(err);
                    if(!data_mongo){
                        var data_mongo = new Data({
                            fitbitid: token.user_id,
                            date: heartSummary.dateTime.toString(),
                            type:'heart',
                            data: data
                        });

                        data_mongo.save(function(err){
                            if(err) console.log(err);
                        });
                    } else {
                        console.log({fitbitCall: "Heart",
                                        success: false,
                                        message: data_mongo.fitbitid + " on " +
                                                    data_mongo.date + " already exists."});
                    }
                });
            }
        }
    }

    for(var m = date_begin; m.isBefore(date_end); m.add(1, 'days')){
        // query whether date in db, if not make request
        Data.findOne({'fitbitid': token.user_id,
                        'date':m.format('YYYY-MM-DD'),
                        'type':'heart'},
                        'fitbitid date', function(err, data_mongo){
            if(err) console.log(err);
            if(!data_mongo){
                // requesting data
                console.log("Heart - requesting:", m.format('YYYY-MM-DD'));
                fitbit.getData('heart',m.format('YYYY-MM-DD'), callback);
            } else {
                console.log({fitbitCall: "Heart",
                                success: false,
                                message: data_mongo.fitbitid + " on " +
                                            data_mongo.date + " already exists."});
            }
        });
    }


    res.send({fitbitCall: "Heartrate", success: true});
});

module.exports = router;
