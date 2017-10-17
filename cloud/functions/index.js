'use strict';

const functions = require('firebase-functions');
const google    = require('googleapis');

const CONFIG_CLIENT_ID     = functions.config().googleapi.client_id;
const CONFIG_CLIENT_SECRET = functions.config().googleapi.client_secret;

const SCOPES             = [ 'https://www.googleapis.com/auth/cloud-language' ];
const FUNCTIONS_REDIRECT = `${process.env.GCLOUD_PROJECT}.firebaseapp.com/oauthCallback`;

const client = new google.auth.OAuth2(CONFIG_CLIENT_ID, CONFIG_CLIENT_SECRET, FUNCTIONS_REDIRECT);

exports.getToken = functions.https.onRequest((request, response) => {
  response.redirect(client.generateAuthUrl({
    access_type : 'offline',
    scope       : SCOPES,
    prompt      : 'consent'
  }));
});

exports.oauthCallback = functions.https.onRequest((request, response) => {
  client.getToken(request.query.code, (error, tokens) => {
    if (error) {
      response.status(400).send(error);
    } else {
      response.status(200).send(tokens);
    }
  });
});
