const puppeteer = require("puppeteer");
const assert = require("assert");
const cas = require("../../cas.js");

(async () => {
    const service = "https://localhost:9859/anything/cas";
    const browser = await puppeteer.launch(cas.browserOptions());
    const page = await cas.newPage(browser);
    await cas.log(`Navigating to ${service}`);
    await cas.gotoLogin(page, service);
    await page.waitForTimeout(2000);
    await cas.click(page, "div .idp span");
    await page.waitForTimeout(4000);
    await cas.screenshot(page);
    await cas.type(page, "#userNameInput", process.env.ADFS_USERNAME, true);
    await cas.type(page, "#passwordInput", process.env.ADFS_PASSWORD, true);
    await page.waitForTimeout(2000);
    await cas.submitForm(page, "#loginForm");
    await page.waitForTimeout(4000);
    await cas.screenshot(page);
    const page2 = await browser.newPage();
    await page2.goto("http://localhost:8282");
    await page2.waitForTimeout(1000);
    await cas.click(page2, "table tbody td a");
    await page2.waitForTimeout(1000);
    const code = await cas.textContent(page2, "div[name=bodyPlainText] .well");
    await page.bringToFront();
    await cas.type(page, "#token", code);
    await cas.submitForm(page, "#fm1");
    await page.waitForTimeout(2000);
    await cas.logPage(page);
    
    const ticket = await cas.assertTicketParameter(page);
    await cas.gotoLogin(page);
    await cas.assertCookie(page);
    await page.waitForTimeout(3000);
    const body = await cas.doRequest(`https://localhost:8443/cas/p3/serviceValidate?service=${service}&ticket=${ticket}&format=JSON`);
    await cas.log(body);
    const json = JSON.parse(body);
    const authenticationSuccess = json.serviceResponse.authenticationSuccess;
    assert(authenticationSuccess.user.includes("casuser@apereo.org"));
    assert(authenticationSuccess.attributes.firstname !== undefined);
    assert(authenticationSuccess.attributes.lastname !== undefined);
    assert(authenticationSuccess.attributes.uid !== undefined);
    assert(authenticationSuccess.attributes.upn !== undefined);
    assert(authenticationSuccess.attributes.username !== undefined);
    assert(authenticationSuccess.attributes.surname !== undefined);
    assert(authenticationSuccess.attributes.email !== undefined);
    await browser.close();
})();
