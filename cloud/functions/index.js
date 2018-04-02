'use strict';

const functions = require('firebase-functions');
const google    = require('googleapis');
const client    = new google.auth.OAuth2(functions.config().googleapi.client_id, functions.config().googleapi.client_secret, `https://us-central1-${process.env.GCLOUD_PROJECT}.cloudfunctions.net/oauthCallback`);

exports.getToken = functions.https.onRequest((request, response) => {
    response.redirect(client.generateAuthUrl({
        access_type : 'offline',
        scope       : [ 'https://www.googleapis.com/auth/cloud-language' ],
        prompt      : 'consent'
    }));
});

exports.oauthCallback = functions.https.onRequest((request, response) => {
    console.log('Request query: ' + request.query);

    client.getToken(request.query.code, (error, tokens) => {
        if (error) {
            response.status(403).send(error);
        } else {
            response.status(200).send(tokens);
        }
    });
});
