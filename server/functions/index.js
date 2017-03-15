let functions = require('firebase-functions');
let admin = require('firebase-admin');
admin.initializeApp(functions.config().firebase);

exports.dispatchMessage = functions.database.ref('/uncommitted/messages/{pushId}')
    .onWrite(event => {
        const message = event.data.current.val();
        const senderUid = message.from;
        const receiverUid = message.to;
        console.log('dispatching' + message.body + ' from' + senderUid + ' to' + receiverUid);
        const promises = [];
        promises.push(admin.database().ref(`/messages/${senderUid}/${receiverUid}`).push(message));
        if (senderUid != receiverUid) {
            promises.push(admin.database().ref(`/messages/${receiverUid}/${senderUid}`).push(message));
        }
        promises.push(event.data.current.ref.remove());
        return Promise.all(promises);
    });