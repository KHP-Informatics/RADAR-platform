var router = require('express').Router();
var config = require('../../config.js');

var oauth2 = require('simple-oauth2')({
    clientID: config.fitbit_consumer_id,
    clientSecret: config.fitbit_consumer_secret,
    site: '',
    authorizationPath: 'https://www.fitbit.com/oauth2/authorize',
    tokenPath: 'https://api.fitbit.com/oauth2/token'
});

var authorization_uri = oauth2.authCode.authorizeURL({
    response_type: 'code',
    redirect_uri: 'http://localhost:3000/auth/fitbit/callback',
    scope: 'sleep social nutrition activity heartrate profile settings',
});

router.get('/', function(req, res){
    res.redirect(authorization_uri);
});

router.get('/callback', function(req, res){

    var code = req.query.code;

    oauth2.authCode.getToken({
        code: code,
        redirect_uri: 'http://localhost:3000/auth/fitbit/callback'
    }, saveToken);

    function  saveToken(error, result){
        if(error){
            console.error('Access Token Error', error.message);
        }
        response = oauth2.accessToken.create(result);
        token = response.token
        //console.log(token);
        process.env.TOKEN = JSON.stringify(token);
        //res.status(200).send(JSON.stringify({access_token: token.access_token}));
        res.redirect('/api/fitbit/profile');
    }
});

module.exports = router;
