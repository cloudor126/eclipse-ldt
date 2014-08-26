---Vicious version of geometry module for testing the formatter
--@module geometry
local
    M
    =
        {}

---A rectangle
--@type rectangle
local
    R={
        x=0,y=0,
        width=100,height=100,
    }

---Move the rectangle
--@function [parent=#rectangle] move
function
R.
    move(
        self,
        x,
        y
    )
        self.x=self.x+x
        self.
            y
            =
                self.y
                +
                y
    end

---Create a new rectangle
--@function [parent=#geometry] newRectangle
function
M.newRectangle(x,y,width,height)
    local
        newrectangle={
            x=x,y=y,
            width=width,height=height
        }

    --set to new rectangle the properties of a rectangle
    setmetatable(
        newrectangle,
        {__index=
            R})
    return
        newrectangle
end

return
    M
