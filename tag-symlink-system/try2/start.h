#ifndef START_H
#define START_H

#include <QDialog>

namespace Ui {
    class start;
}

class start : public QDialog
{
    Q_OBJECT

public:
    explicit start(QWidget *parent = 0);
    ~start();

private slots:
    void on_Button_open_clicked();

    void on_Button_close_clicked();

private:
    Ui::start *ui;
};

#endif // START_H
