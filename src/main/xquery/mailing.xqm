xquery version "3.0";

module namespace mailing='eXmin/mailing';

declare namespace mail="http://exist-db.org/xquery/mail";
declare namespace eXmin="eXmin";

declare function mailing:mail($message) {
	let $props := 
        <properties>
            <property name="mail.debug" value="false"/>
            <property name="mail.smtp.starttls.enable" value="true"/>
            <property name="mail.smtp.auth" value="true"/>
            <property name="mail.smtp.port" value="587"/>
            <property name="mail.smtp.host" value="smtp.gmail.com"/>
            <property name="mail.smtp.user" value="alert@jorsek.com"/>
            <property name="mail.smtp.password"  value="isendthings4u"/>
        </properties>

    (: It throws an exception when there has been some problem composing or sending the e-mails :)
    let $session := mail:get-mail-session( $props )
    return
        mail:send-email($session,$message)
};

declare function mailing:server-down($server) {

	let $props := 
        <properties>
            <property name="mail.smtp.user" value="alert@jorsek.com"/>
        </properties>

    let $message :=
        <mail>
            <from>Alert &lt;{$props//property[@name = "mail.smtp.user"]/@value/string()}&gt;</from>
            { 
                for $address in //eXmin:alert//eXmin:mail
                    return <bcc>{$address/text()}</bcc>
            }
            <subject>Server down</subject>
            <message>
                <text>Server {$server/eXmin:name/text()} do not responce.</text>
                <xhtml>
                    <html>
                        <head>
                            <title>Server {$server/eXmin:name/text()} do not responce.</title>
                        </head>
                        <body>
                            <h1>Server {$server/eXmin:name/text()} do not responce.</h1>
                            <p>{$server/eXmin:url/text()}</p>
                        </body>
                    </html>
                </xhtml>
            </message>
        </mail>
    
    return
		mailing:mail($message)
};

declare function mailing:server-up($server) {

	let $props := 
        <properties>
            <property name="mail.smtp.user" value="alert@jorsek.com"/>
        </properties>

    let $message :=
        <mail>
            <from>Alert &lt;{$props//property[@name = "mail.smtp.user"]/@value/string()}&gt;</from>
            { 
                for $address in //eXmin:alert//eXmin:mail
                    return <bcc>{$address/text()}</bcc>
            }
            <subject>Server up</subject>
            <message>
                <text>Server {$server/eXmin:name/text()} do responce.</text>
                <xhtml>
                    <html>
                        <head>
                            <title>Server {$server/eXmin:name/text()} do responce.</title>
                        </head>
                        <body>
                            <h1>Server {$server/eXmin:name/text()} do responce.</h1>
                            <p>{$server/eXmin:url/text()}</p>
                        </body>
                    </html>
                </xhtml>
            </message>
        </mail>
    
    return
		mailing:mail($message)
};