var express = require('express');
var bodyParser = require('body-parser');
var config = require('./config');

//process.env.DEBUG = true;

var app = express();

// middleware
app.use(bodyParser.json());

// modules
var fitbitAuth = require('./controllers/auth/fitbit-auth.js');
app.use('/auth/fitbit', fitbitAuth);

var fitbitAPI = require('./controllers/api/fitbit-api.js');
app.use('/api/fitbit', fitbitAPI);


app.get('/', function(req, res){

    res.send('<div><h1>SleepSightRescue</h1><br><br>\
                <a href="http://localhost:3000/auth/fitbit">Authenticate</a>\
                <br><a href="http://localhost:3000/api/fitbit/profile">Get Profile</a>');
});

app.listen(3000,function(){
    console.log('SleepSightRescue\nServer listening on', 3000);
});
