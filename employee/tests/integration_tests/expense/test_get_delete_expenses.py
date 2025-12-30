import requests, pytest

class Test_Get_Delete_Expenses:
    @pytest.fixture
    def setup(self):
        BASE_URL = "http://127.0.0.1:5000/api/expenses"

        session = requests.Session()

        session.post(
            "http://127.0.0.1:5000/api/auth/login",
            json={
                "username": "employee1",
                "password": "password123"
            }
        )

        yield BASE_URL, session

        session.close()

    # EI-209
    def test_get_expenses(self, setup):
        response = setup[1].get(setup[0])

        assert response.status_code == 200

        data = response.json()
        assert data["count"] is not None
        assert data["expenses"] is not None
        assert isinstance(data["expenses"], list)

    # EI-210
    def test_delete_expense_positive(self, setup):
        BASE_URL, session = setup

        exp_submission_response = session.post(
            BASE_URL,
            json={
                "amount": 100,
                "description": "This is a test expense"
            }
        )

        exp_to_delete_id = exp_submission_response.json()["expense"]["id"]

        exp_deletion_response = session.delete(
            BASE_URL + f"/{exp_to_delete_id}"
        )

        assert exp_deletion_response.status_code == 200

        data = exp_deletion_response.json()
        assert data["message"] == "Expense deleted successfully"

    # EI-211
    def test_delete_expense_negative(self, setup):
        BASE_URL, session = setup

        exp_deletion_response = session.delete(
            BASE_URL + "/404"
        )

        assert exp_deletion_response.status_code == 404

        data = exp_deletion_response.json()
        assert data["error"] == "Expense not found"