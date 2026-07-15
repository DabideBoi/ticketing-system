import { test, expect } from "@playwright/test";
import { SEEDED_USERS, loginAs, logout } from "./helpers";

function statusBadge(page: import("@playwright/test").Page) {
  return page.getByTestId("ticket-status-badge");
}

test.describe("DB Fix ticket lifecycle", () => {
  test("full happy path: create -> approve -> assign -> ongoing -> for close -> close", async ({ page }) => {
    const title = `DB Fix E2E ${Date.now()}`;

    await loginAs(page, SEEDED_USERS.requestor);
    await page.goto("/tickets/new");
    await page.getByRole("button", { name: "DB Fix" }).click();
    await expect(page.getByText("What happens next")).toBeVisible();
    await expect(page.getByText("Approver", { exact: true })).toBeVisible();

    await page.getByLabel("Title").fill(title);
    await page.getByLabel("Description").fill("Index corrupted on orders table");
    await page.getByRole("button", { name: "Submit Request" }).click();

    await page.waitForURL(/\/tickets\/[0-9a-f-]{36}$/);
    const ticketUrl = page.url();
    await expect(statusBadge(page)).toHaveText("For Approval");
    await logout(page);

    await loginAs(page, SEEDED_USERS.approver);
    await page.goto(ticketUrl);
    await page.getByRole("button", { name: "Approve" }).click();
    await expect(statusBadge(page)).toHaveText("For Assignment");
    await logout(page);

    await loginAs(page, SEEDED_USERS.assigner);
    await page.goto(ticketUrl);
    await page.getByRole("combobox").selectOption({ label: "Jamie Assignee" });
    await page.getByRole("button", { name: "Assign" }).click();
    await expect(statusBadge(page)).toHaveText("Assigned");
    await logout(page);

    await loginAs(page, SEEDED_USERS.assignee);
    await page.goto(ticketUrl);
    await page.getByRole("button", { name: "Start Work" }).click();
    await expect(statusBadge(page)).toHaveText("Ongoing");
    await page.getByRole("button", { name: "Resolve" }).click();
    await expect(statusBadge(page)).toHaveText("For Close");
    await logout(page);

    await loginAs(page, SEEDED_USERS.requestor);
    await page.goto(ticketUrl);
    await page.getByRole("button", { name: "Close Ticket" }).click();
    await expect(statusBadge(page)).toHaveText("Close");

    await expect(page.getByText(title)).toBeVisible();
  });

  test("rejecting a DB Fix ticket terminates the flow as Close", async ({ page }) => {
    const title = `DB Fix Reject E2E ${Date.now()}`;

    await loginAs(page, SEEDED_USERS.requestor);
    await page.goto("/tickets/new");
    await page.getByRole("button", { name: "DB Fix" }).click();
    await page.getByLabel("Title").fill(title);
    await page.getByLabel("Description").fill("Not actually needed");
    await page.getByRole("button", { name: "Submit Request" }).click();
    await page.waitForURL(/\/tickets\/[0-9a-f-]{36}$/);
    const ticketUrl = page.url();
    await logout(page);

    await loginAs(page, SEEDED_USERS.approver);
    await page.goto(ticketUrl);
    await expect(page.getByRole("button", { name: "Reject" })).toBeVisible();
    await page.getByRole("button", { name: "Reject" }).click();
    await expect(statusBadge(page)).toHaveText("Close");
  });
});
