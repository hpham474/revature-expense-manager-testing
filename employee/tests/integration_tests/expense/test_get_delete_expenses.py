import requests, pytest
from flask import Flask

from src.api import expense_controller


class Test_Get_Delete_Expenses:
    @pytest.fixture
    def app_and_client(self, mocker):
        app = Flask(__name__)
        app.config["TESTING"] = True

        app.register_blueprint(expense_controller.expense_bp)

        app.expense_service = mocker.Mock()

        view = app.view_functions["expense.delete_expense"]
        app.view_functions["expense.delete_expense"] = view.__wrapped__

        client = app.test_client()
        return app, client

    @pytest.fixture(autouse=True)
    def mock_auth(self, mocker):
        mocker.patch(
            "src.api.expense_controller.require_employee_auth",
            lambda f: f
        )

        mocker.patch(
            "src.api.expense_controller.get_current_user",
            return_value=mocker.Mock(id=1)
        )

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

        return BASE_URL, session

    # EI-209
    def test_get_expenses(self, setup):
        response = setup[1].get(setup[0])

        assert response.status_code == 200

        data = response.json()
        assert data["count"] is not None
        assert data["expenses"] is not None
        assert isinstance(data["expenses"], list)

    # EI-210
    def test_delete_expense_positive(self, app_and_client):
        app, client = app_and_client

        app.expense_service.delete_expense.return_value = True

        response = client.delete("/api/expenses/1")

        assert response.status_code == 200
        assert response.get_json()["message"] == "Expense deleted successfully"

        app.expense_service.delete_expense.assert_called_once_with(1, 1)

    # EI-211
    def test_delete_expense_negative(self, app_and_client):
        app, client = app_and_client

        app.expense_service.delete_expense.return_value = False

        response = client.delete("/api/expenses/400")

        assert response.status_code == 404
        assert response.get_json()["error"] == "Expense not found"

        app.expense_service.delete_expense.assert_called_once_with(400, 1)