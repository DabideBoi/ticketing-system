import { test, expect } from "@playwright/test";
import { SEEDED_USERS, loginAs } from "./helpers";

test.describe("Role-based access control", () => {
  test("requestor cannot see approve/assign controls on their own ticket", async ({ page }) => {
    const title = `RBAC E2E ${Date.now()}`;

    await loginAs(page, SEEDED_USERS.requestor);
    await page.goto("/tickets/new");
    await page.getByRole("button", { name: "DB Fix" }).click();
    await page.getByLabel("Title").fill(title);
    await page.getByLabel("Description").fill("desc");
    await page.getByRole("button", { name: "Submit Request" }).click();
    await page.waitForURL(/\/tickets\/[0-9a-f-]{36}$/);

    await expect(page.getByRole("button", { name: "Approve" })).toHaveCount(0);
    await expect(page.getByRole("button", { name: "Reject" })).toHaveCount(0);
    await expect(page.getByRole("button", { name: "Assign" })).toHaveCount(0);
    await expect(page.getByText("Actions", { exact: true })).toHaveCount(0);
  });

  test("requestor and assignee cannot access the reporting page", async ({ page }) => {
    await loginAs(page, SEEDED_USERS.requestor);
    await expect(page.getByRole("link", { name: "Reporting" })).toHaveCount(0);

    await page.goto("/reporting");
    await page.waitForURL("**/dashboard");
  });

  test("approver can access reporting; requestor cannot create tickets link", async ({ page }) => {
    await loginAs(page, SEEDED_USERS.approver);
    await expect(page.getByRole("link", { name: "Reporting" })).toBeVisible();
    await expect(page.getByRole("link", { name: "New Request" })).toHaveCount(0);
  });
});
